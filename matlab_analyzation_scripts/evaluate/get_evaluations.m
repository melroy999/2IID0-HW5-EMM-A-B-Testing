function [euclidean_distance_result, results_retained] = get_evaluations(V, beta_original) 
    euclidean_distance_result = get_euclidean_distance(V, beta_original);
    results_retained = length(V);
end