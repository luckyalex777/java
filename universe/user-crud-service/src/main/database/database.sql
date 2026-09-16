create database if not exists javadb character set utf8 collate utf8_general_ci;
create user java_user identified by 'Password1*';
grant select on mysql.* to java_user;
grant select,insert,update on javadb.* to java_user;
grant execute on javadbdb.* to java_user;
