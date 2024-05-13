
VALUES( COOLSTUFF.MOOD('The dog is loud'));


SELECT OUTPUTLINE AS RESPONSE
    FROM TABLE (
            COOLSTUFF.FRENCH_TBL('The dog is loud')
        );
        
SELECT OUTPUTLINE AS RESPONSE
    FROM TABLE (
            COOLSTUFF.NYC_TBL('The dog is loud')
        )
        ;



-- Experiment with insert triggers on the CUSTOMER_INQUIRIES table
DELETE FROM COOLSTUFF.CUSTOMER_INQUIRIES;
INSERT into COOLSTUFF.CUSTOMER_INQUIRIES VALUES('Why have I not received my order?','','','', default);
INSERT into COOLSTUFF.CUSTOMER_INQUIRIES VALUES('The part does not seem to work.','','','', default);
INSERT into COOLSTUFF.CUSTOMER_INQUIRIES VALUES('How much money do I owe?','','','', default);
INSERT into COOLSTUFF.CUSTOMER_INQUIRIES VALUES('I hope to never see you again','','','', default);
INSERT into COOLSTUFF.CUSTOMER_INQUIRIES VALUES('This product is the best!','','','', default);
INSERT into COOLSTUFF.CUSTOMER_INQUIRIES VALUES('Thank you for your help today!','','','', default);
INSERT into COOLSTUFF.CUSTOMER_INQUIRIES VALUES('Unfortunately, I cannot afford it','','','', default);
select * from COOLSTUFF.CUSTOMER_INQUIRIES order by id desc;
STOP;


-- Call watsonX with HTTP (the)
values QSYS2.HTTP_POST(
    'https://us-south.ml.cloud.ibm.com/ml/v1/text/generation?version=2023-07-07',
    '{ "model_id": "meta-llama/llama-2-13b-chat","input": "What is the capital of Boston?", "parameters": { "max_new_tokens": 100, "time_limit": 1000 },"space_id": "<your_spaceid_here>" }',
    '{"headers":{"Authorization":"Bearer <your_token_here>", "Content-Type": "application/json", "Accept": "application/json"}}'
);



-- CAll WatsonX with UDF (that uses HTTP)
VALUES COOLSTUFF.WATSONXAIHTTP('why are armadillos so cute?')
