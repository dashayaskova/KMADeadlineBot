# KMADeadlineBot
* telegram bot - [@KMADeadlineBot](https://t.me/KMADeadlineBot)
* telegram chat - [chat](https://t.me/joinchat/Ept6GE7IV8xdbFVbA9nEkQ)
* trello board - [telegram bot board](https://trello.com/b/WTZ2zEcV/deadlines-telegram-bot)

## how to install?
1. import this project into [eclipse](https://eclipse.org)
1. install [mysql server](https://mysql.com)
1. run next code in your 'mysql command line client':
```mysql
create database KMADeadlineBot;

use KMADeadlineBot;

create table user (
  user_id bigint not null,
    primary key(user_id)
  );
    
create table global_admin(
  user_id bigint not null,
    foreign key(user_id) references user(user_id)
    );

create table community(
  community_id bigint not null auto_increment,
    name_sn char(255) not null,
    date_created_dt date,
    primary key(community_id)
  );
    
create table deadline(
  deadline_id bigint not null auto_increment,
    date_of_deadline datetime not null,
    community_id bigint not null,
    primary key(deadline_id),
    foreign key(community_id) references community(community_id)
    );
    
create table deadline_message(
  deadline_message_id bigint not null auto_increment,
    message_id bigint not null,
    chat_id bigint not null,
    deadline_id bigint not null,
    primary key(deadline_message_id),
    foreign key(deadline_id) references deadline(deadline_id)
  );

create table deadline_date(
  deadline_date_id bigint not null auto_increment,
    user_id bigint not null,
    deadline_id bigint not null,
    date datetime not null,
    primary key(deadline_date_id),
    foreign key(user_id) references user(user_id),
    foreign key(deadline_id) references deadline(deadline_id)
  );

create table user_community(
  user_community_id bigint not null auto_increment,
    user_id bigint not null,
    community_id bigint not null,
    primary key(user_community_id),
    foreign key(user_id) references user(user_id),
    foreign key(community_id) references community(community_id)
  );
    
create table admin_community(
  admin_community_id bigint not null auto_increment,
    user_id bigint not null,
    community_id bigint not null,
    primary key(admin_community_id),
    foreign key(user_id) references user(user_id),
    foreign key(community_id) references community(community_id)
);
```

## Improvements
1. add libraries with telegram bot api and mysql connection api
1. create classes `User`, `Community`, `Deadline`
1. implement methods in classes `User`, `Community`, `Deadline`
1. create `session` package with classes `Session`, `SessionContainer` and interface `UpdateListener`
