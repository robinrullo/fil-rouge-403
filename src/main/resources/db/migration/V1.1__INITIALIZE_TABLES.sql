create table public.pois
(
    id       bigserial    not null,
    name     varchar(255) not null,
    address  varchar(255),
    geometry geometry(Point, 4326),
    primary key (id)
);

create sequence public.hibernate_sequence;
