%Determine the score of the dataset using the cook's distance.
function score = get_cooks_distance(V, V_original, beta_original) 
    %calculate the beta of the subgroup.
    beta_subgroup = get_beta_vector(V);
    
    %Calculate the beta difference.
    beta_difference = beta_subgroup - beta_original;
    
    X = [ones(length(V_original),1) V_original(:,1:end-1)];
    Y = V_original(:,end);
    
    [~, p] = size(V_original);
    
    %Get the difference between the real Y values and the expected Y values.
    e = Y - X * beta_original;
    
    %Calculate s^2.
    s_squared = (e.' * e) / (length(V_original) - p);

    %Calculate the score.
    score = (beta_difference.' * (X.' * X) * beta_difference) / (7 * s_squared);
end