package com.ibm.jesseg;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

import com.ibm.as400.access.AS400JDBCDataSource;

import dev.langchain4j.model.openai.OpenAiChatModel;

import org.apache.camel.component.jt400.Jt400Endpoint;

import java.io.IOException;

/**
 * A Camel Application that routes messages from an IBM i message queue to
 * email.
 */
public class MainApp {

    private static class AIRouteBuilder extends RouteBuilder {

        private final AIGenerator m_generator;
        private final String m_aiPrefix;
        private final String m_routeName;

        public AIRouteBuilder(AIGenerator _gen, String _aiPrefix, String _routeName) {
            m_generator = _gen;
            m_aiPrefix = _aiPrefix;
            m_routeName = _routeName;
        }

        @Override
        public void configure() throws Exception {
            from("direct:"+m_routeName)
                    .process((exchange) -> {
                        String input = exchange.getIn().getBody(String.class);
                        System.out.println("in: " + input);
                        String answer = m_generator.generate(m_aiPrefix + input).trim().replaceAll("^\\.+", "");
                        answer = answer.substring(0, Math.min(64000, answer.length()));
                        System.out.println("out: " + answer+"\n---");
                        exchange.getIn().setBody(answer);
                    })
                    .convertBodyTo(String.class);
        }
    }

    private static class DQWorkItem {
        private String m_inQUrl;
        private String m_outQUrl;
        private AIRouteBuilder m_routeBuilder;

        public DQWorkItem(final String _inQUrl, final String _outQUrl, AIRouteBuilder _routeBuilder) {
            m_inQUrl = _inQUrl;
            m_outQUrl = _outQUrl;
            m_routeBuilder = _routeBuilder;
        }
        public void install(CamelContext _c) throws Exception {
            _c.addRoutes(new RouteBuilder() {
                @Override
                public void configure() {
                    from(m_inQUrl)
                            .process((exchange) -> {
                                Object hdr = exchange.getIn().getHeader(Jt400Endpoint.KEY);
                                exchange.getIn().setHeader("DQFUNC",
                                        new String((byte[]) hdr, "Cp037").replaceAll(":.*$", ""));
                            })
                            .to("direct:"+m_routeBuilder.m_routeName)
                            .to(m_outQUrl);
                }
            });
        }

    }

    interface AIGenerator {
        public String generate(String _in) throws IOException;
    }

    public static void main(final String... args) throws Exception {
        // Standard for a Camel deployment. Start by getting a CamelContext object.
        CamelContext context = new DefaultCamelContext();
        System.out.println("Apache Camel version " + context.getVersion());

        String username = "*CURRENT";
        String password = "*CURRENT";
        String hostname = "localhost";

        AS400JDBCDataSource ds = new AS400JDBCDataSource(hostname, username, password);
        context.getRegistry().bind("ibmids", ds);
        String compAndAuthinfo = String.format("jt400://%s:%s@%s", username, password, hostname);
        boolean isWatsonX = false;

        final AIGenerator model;
        if (isWatsonX) {
            final WatsonXAsker wx = WatsonXAsker.getWithHardcodedDefaults();
            model = new AIGenerator() {
                @Override
                public String generate(String _in) throws IOException {
                    return wx.generate(_in);
                }
            };
        } else {
            final OpenAiChatModel openai = OpenAiChatModel.builder()
                    .apiKey("demo")
                    .build();
            model = new AIGenerator() {
                @Override
                public String generate(String _in) throws IOException {
                    return openai.generate(_in);
                }
            };
        }

        final AIRouteBuilder llmRoute = new AIRouteBuilder(model, "", "llm_plain");
        context.addRoutes(llmRoute);
        final String dtaqUriRead = compAndAuthinfo
                + "/qsys.lib/coolstuff.lib/llmq.DTAQ?format=binary&keyed=true&searchKey=&readTimeout=-1&searchType=NE";
        final String dtaqUriWrite = compAndAuthinfo + "/qsys.lib/coolstuff.lib/llmq2.DTAQ?format=binary&keyed=true";
        new DQWorkItem(dtaqUriRead, dtaqUriWrite, llmRoute).install(context);


        final AIRouteBuilder frenchRoute = new AIRouteBuilder(model, "Translate the following text into French (include only the translation in your response):\n", "french");
        context.addRoutes(frenchRoute);
        final String frenchDtaqUriRead = compAndAuthinfo
                + "/qsys.lib/coolstuff.lib/frenchq.DTAQ?format=binary&keyed=true&searchKey=&readTimeout=-1&searchType=NE";
        final String frenchDtaqUriWrite = compAndAuthinfo
                + "/qsys.lib/coolstuff.lib/frenchq2.DTAQ?format=binary&keyed=true";
        new DQWorkItem(frenchDtaqUriRead, frenchDtaqUriWrite, frenchRoute).install(context);


        final AIRouteBuilder nycRoute = new AIRouteBuilder(model, "Translate the following text as how a rude New Yorker would say it (include only the translation in your response):\n", "nyc");
        context.addRoutes(nycRoute);
        final String nycDtaqUriRead = compAndAuthinfo
                + "/qsys.lib/coolstuff.lib/nycq.DTAQ?format=binary&keyed=true&searchKey=&readTimeout=-1&searchType=NE";
        final String nycDtaqUriWrite = compAndAuthinfo + "/qsys.lib/coolstuff.lib/nycq2.DTAQ?format=binary&keyed=true";
        new DQWorkItem(nycDtaqUriRead, nycDtaqUriWrite, nycRoute).install(context);

        final AIRouteBuilder moodRoute = new AIRouteBuilder(model, "Please do a mood analysis. Respond with only a single word. Choices are 'angry', 'sad', 'thankful', 'happy', 'neutral', 'confused'. What word best describes the mood of the following text?\n\n", "mood");
        context.addRoutes(moodRoute);
        final String moodDtaqUriRead = compAndAuthinfo
                + "/qsys.lib/coolstuff.lib/moodq.DTAQ?format=binary&keyed=true&searchKey=&readTimeout=-1&searchType=NE";
        final String moodDtaqUriWrite = compAndAuthinfo + "/qsys.lib/coolstuff.lib/moodq2.DTAQ?format=binary&keyed=true";
        new DQWorkItem(moodDtaqUriRead, moodDtaqUriWrite, moodRoute).install(context);

        // This actually "starts" the route, so Camel will start monitoring and routing
        // activity here.
        context.start();

        // Since this program is designed to just run forever (until user cancel), we
        // can just sleep the
        // main thread. Camel's work will happen in secondary threads.
        Thread.sleep(Long.MAX_VALUE);
        context.stop();
        context.close();
    }
}
