#!/QOpenSys/pkgs/bin/bash

[ -e /qsys.lib/COOLSTUFF.LIB ] || system -v "RUNSQL SQL('create schema coolstuff') COMMIT(*NONE)"
[ -e /qsys.lib/COOLSTUFF.LIB/LLMQ2.DTAQ ] || system -v "CRTDTAQ DTAQ(COOLSTUFF/LLMQ2) MAXLEN(64512) SEQ(*KEYED) KEYLEN(100) SENDERID(*YES) SIZE(*MAX2GB)"
[ -e /qsys.lib/COOLSTUFF.LIB/LLMQ.DTAQ ] || system -v "CRTDTAQ DTAQ(COOLSTUFF/LLMQ) MAXLEN(64512) SEQ(*KEYED) KEYLEN(100) SENDERID(*YES) SIZE(*MAX2GB)"

[ -e /qsys.lib/COOLSTUFF.LIB/FRENCHQ2.DTAQ ] || system -v "CRTDTAQ DTAQ(COOLSTUFF/FRENCHQ2) MAXLEN(64512) SEQ(*KEYED) KEYLEN(100) SENDERID(*YES) SIZE(*MAX2GB)"
[ -e /qsys.lib/COOLSTUFF.LIB/FRENCHQ.DTAQ ] || system -v "CRTDTAQ DTAQ(COOLSTUFF/FRENCHQ) MAXLEN(64512) SEQ(*KEYED) KEYLEN(100) SENDERID(*YES) SIZE(*MAX2GB)"

[ -e /qsys.lib/COOLSTUFF.LIB/NYCQ2.DTAQ ] || system -v "CRTDTAQ DTAQ(COOLSTUFF/NYCQ2) MAXLEN(64512) SEQ(*KEYED) KEYLEN(100) SENDERID(*YES) SIZE(*MAX2GB)"
[ -e /qsys.lib/COOLSTUFF.LIB/NYCQ.DTAQ ] || system -v "CRTDTAQ DTAQ(COOLSTUFF/NYCQ) MAXLEN(64512) SEQ(*KEYED) KEYLEN(100) SENDERID(*YES) SIZE(*MAX2GB)"

[ -e /qsys.lib/COOLSTUFF.LIB/MOOD2.DTAQ ] || system -v "CRTDTAQ DTAQ(COOLSTUFF/MOODQ2) MAXLEN(64512) SEQ(*KEYED) KEYLEN(100) SENDERID(*YES) SIZE(*MAX2GB)"
[ -e /qsys.lib/COOLSTUFF.LIB/MOODQ.DTAQ ] || system -v "CRTDTAQ DTAQ(COOLSTUFF/MOODQ) MAXLEN(64512) SEQ(*KEYED) KEYLEN(100) SENDERID(*YES) SIZE(*MAX2GB)"

system -v "RUNSQLSTM SRCSTMF('$PWD/setup.sql')" | grep -E '^MSG|^SQL'