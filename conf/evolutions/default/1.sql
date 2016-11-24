# --- !Ups 
create table serietemporale (
	id INTEGER PRIMARY KEY,
	nome VARCHAR NOT NULL,
	etichetta VARCHAR NOT NULL,
	valore VARCHAR NOT NULL
); 
# --- !Downs 
drop table serietemporale ; 
