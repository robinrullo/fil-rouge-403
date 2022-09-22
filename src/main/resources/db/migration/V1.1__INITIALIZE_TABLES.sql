create table public.waypoints_itineraries
(
    itinerary_id bigint not null,
    poi_id       bigint not null,
    constraint waypoints_itineraries_pkey primary key (itinerary_id, poi_id)
);

create table public.pois
(
    id       bigserial    not null,
    name     varchar(255) not null,
    address  varchar(255) not null,
    geometry Geometry,
    primary key (id)
);

create table public.itineraries
(
    id       int8         not null,
    geometry Geometry,
    name     varchar(255) not null,
    primary key (id)
);

alter table public.waypoints_itineraries
    add constraint fk_poi_itinerary foreign key (itinerary_id) references public.itineraries;

create sequence public.hibernate_sequence;
