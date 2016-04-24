-- This script finds the top stars in IMDb (those with the most "good movies")!

-- Note the last column now has gender
raw_roles = LOAD 'hdfs://172.17.0.2:9000/user/root/uhadoop/vtomasv/imdb-stars-g-100k.tsv' USING PigStorage('\t') AS (star, title, year, num, type, episode, billing, char, gender);
-- Later you can change the above file to 'hdfs://cm:9000/uhadoop/shared/imdb/imdb-stars-g.tsv' to see the full output


raw_ratings = LOAD 'hdfs://172.17.0.2:9000/user/root/uhadoop/vtomasv/imdb-ratings.tsv' USING PigStorage('\t') AS (dist, votes, score, title, year, num, type, episode);

--------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------
-- Now to implement the script

peliculas = FILTER raw_roles BY type == 'THEATRICAL_MOVIE';
ratings = FILTER raw_ratings BY type == 'THEATRICAL_MOVIE';

peliculasBuenas = FILTER ratings BY ((votes >= 1000) AND ( score >= 8.0));

todasLasPelisConActores = FOREACH peliculas       GENERATE CONCAT(title, '##' , year , '##', num) AS movie, star, gender ;

todasLasPelisBuenas =     FOREACH peliculasBuenas GENERATE CONCAT(title, '##' , year , '##', num) AS movie, votes, score ;

pelisFiltradasConUnActorPorPeli = DISTINCT todasLasPelisConActores;

PelisConRanking = JOIN pelisFiltradasConUnActorPorPeli by movie LEFT OUTER, todasLasPelisBuenas by movie;


pelisFormateados  = FOREACH PelisConRanking GENERATE $0 AS movie, $1 AS star, $2 AS gender, (($3 IS NULL) ? '0' : $3) AS movie_2, (($4 IS NULL) ? '0' : $4) AS votes, (($5 IS NULL) ? '0' : $5) AS score ;

actoresPeliculasMalas = FILTER pelisFormateados BY movie_2 == '0';

actoresPeliculasBuenas = FILTER pelisFormateados BY movie_2 != '0';

hombres = FILTER actoresPeliculasBuenas BY gender == 'MALE';
hombresAgrupado = GROUP hombres by (star);

mujeres = FILTER actoresPeliculasBuenas BY gender == 'FEMALE';
mujeresAgrupado = GROUP mujeres by (star);

mejoresActores  = FOREACH hombresAgrupado GENERATE flatten(group) , COUNT ( hombres.movie ) AS count;
mejoresActrices = FOREACH mujeresAgrupado GENERATE flatten(group) , COUNT ( mujeres.movie ) AS count;


hombresMalos = FILTER actoresPeliculasMalas BY gender == 'MALE';
hombresAgrupadoMalos = GROUP hombresMalos by (star);

mujeresMalos = FILTER actoresPeliculasMalas BY gender == 'FEMALE';
mujeresAgrupadoMalos = GROUP mujeresMalos by (star);

malosActores  = FOREACH hombresAgrupadoMalos GENERATE flatten(group) , 0 AS count;
malasActrices = FOREACH mujeresAgrupadoMalos GENERATE flatten(group) , 0 AS count;

todosHombres = UNION mejoresActores, malosActores;
todasMujeres = UNION mejoresActrices, malasActrices;

hombresOrdenados  =   ORDER todosHombres BY count  DESC;
mujeresOrdenados  =   ORDER todasMujeres  BY count  DESC;


STORE hombresOrdenados INTO '/uhadoop/vtomasv/h_10/';

STORE mujeresOrdenados INTO '/uhadoop/vtomasv/m_10/';

--------------------------------------------------------------------------------------
--------------------------------------------------------------------------------------
