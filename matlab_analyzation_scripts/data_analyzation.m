%%%% Prepare the required data. In this data, all '?' have been replaced by
%%%% -1 priorly.
V = csvread('speed_dating_numeric_targets.csv', 1, 0);

%Set the seed so that we can reproduce the results.
rng(1);

%Get all indices of records that contain at least one missing value.
missing_values = any(V == -1, 2);

%Select all rows that are not present in m.
V_minus_m = V(~missing_values,:);
[n, m] = size(V_minus_m);

%Get the locations of the missing values.
V_missing_value_locations = logical(V == -1);

%Calculate the beta vector for the original vector.
beta_original = get_beta_vector(V_minus_m);

%%%% Calculate merit of methods.
%We randomly remove roughly (8378-7017)/8378=16% of the values within the V_minus_m
%array. As we work with randomness, we will repeat this process 'repetitions' times, using the same dataset for each of the methods.
repetitions = 500;
euclidean_distance_results = zeros(repetitions, 10);
cooks_distance_results = zeros(repetitions, 10);
results_retained = zeros(repetitions, 10);

for i = 1:repetitions
    %Create an experiment matrix.
    V_experiment = create_experiment_dataset(V_minus_m, V_missing_value_locations);
    
    %Calculate the mean values and standard deviation values of each column, 
    %ignoring the -1 unknown values.
    V_experiment_attribute_mean = zeros(m, 1);
    V_experiment_attribute_std = zeros(m, 1);
    V_experiment_attribute_mode = zeros(m, 1);
    for j = 1:7
        V_experiment_attribute_mean(j) = mean(V_experiment(V_experiment(:,j)~=-1));
        V_experiment_attribute_std(j) = std(V_experiment(V_experiment(:,j)~=-1));
        V_experiment_attribute_mode(j) = mode(V_experiment(V_experiment(:,j)~=-1));
    end
    
    %Evaluate all the methods.
    column = 1;
    ignore_missing_values_V = ignore_missing_values(V_experiment);
    [euclidean_distance_results(i,column), cooks_distance_results(i,column), results_retained(i,column)] = get_evaluations(ignore_missing_values_V, V_minus_m, beta_original);
    
    column = column + 1;
    attribute_average_substitution_V = attribute_average_substitution(V_experiment, V_experiment_attribute_mean);
    [euclidean_distance_results(i,column), cooks_distance_results(i,column), results_retained(i,column)] = get_evaluations(attribute_average_substitution_V, V_minus_m, beta_original);
    
    column = column + 1;
    record_average_substitution_V = record_average_substitution(V_experiment);
    [euclidean_distance_results(i,column), cooks_distance_results(i,column), results_retained(i,column)] = get_evaluations(record_average_substitution_V, V_minus_m, beta_original);
    
    column = column + 1;
    attribute_majority_vote_V = attribute_majority_vote(V_experiment, V_experiment_attribute_mode);
    [euclidean_distance_results(i,column), cooks_distance_results(i,column), results_retained(i,column)] = get_evaluations(attribute_majority_vote_V, V_minus_m, beta_original);
    
    column = column + 1;
    record_majority_vote_V = record_majority_vote(V_experiment);
    [euclidean_distance_results(i,column), cooks_distance_results(i,column), results_retained(i,column)] = get_evaluations(record_majority_vote_V, V_minus_m, beta_original);
    
    column = column + 1;
    record_minimum_substitution_V = record_minimum_substitution(V_experiment);
    [euclidean_distance_results(i,column), cooks_distance_results(i,column), results_retained(i,column)] = get_evaluations(record_minimum_substitution_V, V_minus_m, beta_original);
    
    column = column + 1;
    record_maximum_substitution_V = record_maximum_substitution(V_experiment);
    [euclidean_distance_results(i,column), cooks_distance_results(i,column), results_retained(i,column)] = get_evaluations(record_maximum_substitution_V, V_minus_m, beta_original);
    
    column = column + 1;
    rating_target_averaging_V = rating_target_averaging(V_experiment);
    [euclidean_distance_results(i,column), cooks_distance_results(i,column), results_retained(i,column)] = get_evaluations(rating_target_averaging_V, V_minus_m, beta_original);
    
    %Get the similarity mapping.
    similarity_mapping = create_similarity_mapping(V_experiment);
    
    column = column + 1;
    attribute_similarity_V = attribute_similarity(V_experiment, similarity_mapping);
    [euclidean_distance_results(i,column), cooks_distance_results(i,column), results_retained(i,column)] = get_evaluations(attribute_similarity_V, V_minus_m, beta_original);
    
    %Get the similarity mapping.
    similarity_mapping = create_similarity_mapping_full(V_experiment);
    
    column = column + 1;
    attribute_similarity_V = attribute_similarity(V_experiment, similarity_mapping);
    [euclidean_distance_results(i,column), cooks_distance_results(i,column), results_retained(i,column)] = get_evaluations(attribute_similarity_V, V_minus_m, beta_original);
end

euclidean_distance_mean = mean(euclidean_distance_results)
euclidean_distance_std = std(euclidean_distance_results)

cooks_distance_mean = mean(cooks_distance_results)
cooks_distance_std = std(cooks_distance_results)

results_retained_mean = mean(results_retained)
results_retained_std = std(results_retained)

%Make the result_retained a percentage. 
percentage_results_retained = results_retained / length(V_minus_m);

percentage_results_retained_mean = mean(percentage_results_retained)
percentage_results_retained_std = std(percentage_results_retained)