This project relay on tutorial from site: http://www.androidhive.info/2012/05/how-to-connect-android-with-php-mysql/
Only small changes was made to cleanup Exceptions

Folder "php_files" contain all necessary PHP files 
Project require MySQL database installed on localhost:

CREATE DATABASE androidhive;

CREATE TABLE products(
pid int(11) primary key auto_increment,
name varchar(100) not null,
price decimal(10,2) not null,
description text,
created_at timestamp default now(),
updated_at timestamp
);