# -----------------------------------------------------------------------
# CONFIGURATION VARIABLES
shex_files_path='/gfs/projects/genwiki/shapes/';
shex_file_names=(active_site anatomical_structure binding_site biological_pathway biological_process chemical_compound chromosome disease gene mechanism_of_action medication molecular_funcion pharmaceutical_product pharmacologic_action protein protein_domain protein_family ribosomal_RNA sequence_variant supersecondary_structure symptom taxon therapeutic_use)
results_files_path='/gfs/projects/genwiki/results/';
# -----------------------------------------------------------------------
# DO NOT MODIFY ANYTHING BELOW THIS LINE!!!

echo 'Welcome to WDSub GenWiki!';
echo 'This script will create a subset for each defined shape.';

echo 'Pulling wdsub docker image...';
docker pull wesogroup/wdsub:0.0.11;

for shex_file_name in "${shex_file_names[@]}"
do
    echo "Creating subset for ${shex_files_path}${shex_file_name}.shex";
    docker run -v /gfs/projects/weso-scholia/dumps/:/data \
        -v $shex_files_path:/shex \
        -v $results_files_path:/dumps \
        wesogroup/wdsub:0.0.11 dump \
        -o /dumps/result_$shex_file_name.json.gz \
        -s /shex/$shex_file_name.shex \
        /data/latest-all.json.gz;
    echo "Created subset for ${shex_files_path}${shex_file_name}.shex";
done