function V = create_experiment_dataset(V, V_original_missing_value_locations) 
    %note down the size of the original matrix.
    [n,~] = size(V);
   
    %Take 'n' numbers from a permutation of the values 1 to
    %V_original_missing_value_locations's size.
    permutation = randperm(length(V_original_missing_value_locations), n);
    
    %We only want to retain the rows denoted by permutation.
    missing_value_mask = V_original_missing_value_locations(permutation,:);
    
    %Set the values positive in this mask to minus one.
    V(missing_value_mask) = -1;
end