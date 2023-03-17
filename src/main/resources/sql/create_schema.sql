begin;

create table if not exists "User" (
    id serial primary key,
    firstName varchar(20) not null,
    lastName varchar(20) not null,
    "password" varchar(200) not null,
    salt varchar(32) not null
);


create table if not exists Token(
    token varchar(255) primary key,
    userID int,
    foreign key(userID) references "User"(id)
);

create table if not exists DeviceGroup(
    id serial primary key,
    name varchar(30) not null,
    description varchar(255) not null
);

create table if not exists Device(
    id serial primary key,
    name varchar(20) not null,
    streamURL varchar(200) not null, --The max length of a RTSP URL is 200 bytes
    description varchar(255) not null,
    userID int,
    groupID int,
    foreign key (userID) references "User"(id),
    foreign key (groupID) references DeviceGroup(id)
);

create table if not exists Metric(
    deviceID int,
    startTime timestamp not null,
    endTime timestamp not null,
    peopleCount int not null default 0,
    foreign key (deviceID) references Device(id),
    primary key (deviceID, startTime, endTime)
);

create table if not exists ProcessedStream(
    deviceID int primary key,
    streamURL varchar(200) not null,
    foreign key (deviceID) references Device(id)
);



commit;