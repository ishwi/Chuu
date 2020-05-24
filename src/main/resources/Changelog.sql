alter table user add column  private_update TINYINT(1) NOT NULL DEFAULT FALSE;
alter table user add column  notify_image TINYINT(1) NOT NULL DEFAULT TRUE;
alter table user add column  additional_embed TINYINT(1) NOT NULL DEFAULT false ;
alter table guild add column  additional_embed TINYINT(1) NOT NULL DEFAULT false ;


