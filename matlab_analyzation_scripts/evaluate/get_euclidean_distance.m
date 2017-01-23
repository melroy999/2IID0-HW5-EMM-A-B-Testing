%Determine the score of the dataset using the euclidean distance.
function score = get_euclidean_distance(V, beta_original) 
    %calculate the beta of the subgroup.
    beta_subgroup = get_beta_vector(V);
    
    %Calculate the beta score.
    score = 0;
    for i=1:6
        score = score + (beta_subgroup(i) - beta_original(i))^2;
    end
end