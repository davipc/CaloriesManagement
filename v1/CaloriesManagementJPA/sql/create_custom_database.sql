-- RAN AND TESTED PostgreSQL 9.5 @ ON WINDOWS 7 

psql -d postgres -U postgres -c "create user toptal_calories with password 'password';"
psql -d postgres -U toptal_calories -c "select 'test';"
psql -d postgres -U postgres -c "create database toptal_calories template postgres;"
psql -d postgres -U postgres -c "grant all privileges on database toptal_calories to toptal_calories;"

psql -U toptal_calories -d toptal_calories

CREATE TABLE test(ID VARCHAR(32), TEST_MSG VARCHAR(64));
insert into test values ('1','SOME CRAZY MESSAGE');
select * from test;
drop table test;
\q

