select * from meal
where user_id = 85
order by user_id, meal_date, meal_time;

select meal.id as MEAL_ID, u.login as USER, u.id as USER_ID, role.name
from meal, app_user as u, user_role, role
where meal.user_id = u.id
and u.id = user_role.user_id
and user_role.role_id = role.id
and role.name in ('DEFAULT', 'MANAGER', 'ADMIN') 

select u.login, u.id, r.name, r.id
from app_user as u , user_role as ur , role as r
where u.id = ur.user_id and ur.role_id = r.id;



select * from app_user order by id;

select * from user_role;

select * from role;

SELECT sum(numbackends) FROM pg_stat_database;

--drop table Meal cascade;
--drop table User_Role cascade;
--drop table App_User cascade;
--drop table Role cascade;