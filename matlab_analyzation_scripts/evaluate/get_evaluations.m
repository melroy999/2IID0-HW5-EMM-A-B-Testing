function [euclidean_distance_result, cooks_distance_result, results_retained] = get_evaluations(V, V_original, beta_original) 
    euclidean_distance_result = get_euclidean_distance(V, beta_original);
    cooks_distance_result = get_cooks_distance(V, V_original, beta_original);
    results_retained = length(V);
end