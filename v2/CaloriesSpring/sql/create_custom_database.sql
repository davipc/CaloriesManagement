-- RAN AND TESTED PostgreSQL 9.5 @ ON WINDOWS 7 

psql -d postgres -U postgres -c "create user toptal_calories_spring with password 'password';"
psql -d postgres -U toptal_calories -c "select 'test';"
psql -d postgres -U postgres -c "create database toptal_calories_spring template postgres;"
psql -d postgres -U postgres -c "grant all privileges on database toptal_calories_spring to toptal_calories_spring;"

psql -U toptal_calories_spring -d toptal_calories_spring

CREATE TABLE test(ID VARCHAR(32), TEST_MSG VARCHAR(64));
insert into test values ('1','SOME CRAZY MESSAGE');
select * from test;
drop table test;
\q

