-- This script finds the actors/actresses that have acted together the most times

-- Input: (actor, title, year, num, type, episode, billing, char)
raw = LOAD 'hdfs://172.17.0.4:9000/user/root/uhadoop/vtomasv/imdb-stars-100k.tsv' USING PigStorage('\t') AS (actor, title, year, num, type, episode, billing, char);
-- Later you can change the above file to 'hdfs://cm:9000/uhadoop/shared/imdb/imdb-stars.tsv' to see the full output

-- Now to implement the script

-- Line 1: Filter raw to make sure type equals 'THEATRICAL MOVIE'

movies = FILTER raw BY type == 'THEATRICAL_MOVIE';

-- Line 2: Generate new relation with full movie name (concatenating title+##+year+##+num) and actor

full_movies = FOREACH movies GENERATE CONCAT(title, '##' , year , '##', num) AS movie , actor;

-- Line 3 + 4: Generate the co-star pairs
-- Copio la peliculas entre si y realizo un Self Join

full_movies_copy = FOREACH full_movies GENERATE movie , actor;

actor_pairs = JOIN full_movies BY movie,  full_movies_copy BY movie;

-- Line 5: filter to ensure that the first co-star is lower alphabetically than the second

filter_actor_pairs =   FILTER actor_pairs BY full_movies::actor < full_movies_copy::actor;


-- Line 6: concatenate the co-stars into one column

filteres_costart = FOREACH filter_actor_pairs GENERATE CONCAT(full_movies::actor, '##',full_movies_copy::actor ) as costar;

-- Line 7: group the relation by co-stars

costart_groups =  GROUP  filteres_costart BY costar;

-- Line 8: count each group of co-stars

costart_count =  FOREACH costart_groups GENERATE COUNT ( $1 )  AS count, group  AS costar;

-- Line 9: order the count in descending order

ordered_costart_count  =   ORDER costart_count  BY $0  DESC;

-- output the final count
STORE ordered_costart_count INTO '/user/root/uhadoop/vtomasv/imdb-costars-100k_2/';
~                                                                                    
