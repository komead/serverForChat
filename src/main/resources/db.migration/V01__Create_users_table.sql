create table users
(
    id int auto_increment,
    username varchar(16) unique not null,
    password varchar(128) not null,
    primary key (id)
);