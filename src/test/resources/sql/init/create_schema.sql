begin;

create table if not exists UserRole(
                                       id serial primary key,
                                       "role" varchar(30) not null
);

insert into UserRole("role") values ('USER'),('ADMIN'), ('MODERATOR');

create table if not exists "user" (
                                      id serial primary key,
                                      first_name varchar(20) not null,
                                      last_name varchar(20) not null,
                                      "role" int not null default 1,
                                      password_hash varchar(200) not null,
                                      password_salt varchar(32) not null,
                                      email varchar(100) constraint email_invalid check(email ~* '^[A-Z0-9._%-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,4}$') unique,
                                      foreign key ("role") references UserRole(id)
);


create table if not exists SessionToken(
                                           token varchar(255) primary key,
                                           expiration timestamp not null,
                                           userID int,
                                           foreign key(userID) references "user"(id)
);

create table if not exists DeviceGroup(
                                          id serial primary key,
                                          name varchar(30) not null,
                                          description varchar(255)
);

create table if not exists Device(
                                     id serial primary key,
                                     name varchar(20) not null,
                                     streamURL varchar(200) not null, --The max length of a RTSP URL is 200 bytes
                                     description varchar(255),
                                     processingState varchar(15) not null default 'INACTIVE',
                                     pending_update boolean NOT null default false,
                                     processedStreamUrl varchar(200),
                                     scheduled_for_deletion boolean NOT null default false
);

create table if not exists DeviceGroupLink(
                                              deviceID int,
                                              groupID int,
                                              foreign key (deviceID) references Device(id),
                                              foreign key (groupID) references  DeviceGroup(id),
                                              primary key (deviceID,groupID)
);

create table if not exists Metric(
                                     deviceID int,
                                     start_time timestamp not null,
                                     end_time timestamp not null,
                                     peopleCount int not null default 0,
                                     foreign key (deviceID) references Device(id),
                                     primary key (deviceID, start_time)
);

commit;
