function mapping = create_similarity_mapping(V)
    %Take the entries in V that do not contain any missing values.
    well_defined_values = V(~any(V == -1, 2),:);

    %Get the size of the matrix, specifically the column count.
    [~,m] = size(well_defined_values);
    
    %Create the result matrix.
    mean_differences = zeros(m);
    for i=1:m
        for j=1:m
            %We are only interested in the absolute value.
            mean_differences(i,j) = abs(mean(well_defined_values(:,i) - well_defined_values(:,j)));
        end
    end
    
    %Remove all 0 values.
    mean_differences(mean_differences == 0) = intmax;
    
    %Create an empty mapping.
    mapping = ones(m, 2);
    
    %We want to find the minimum for each row.
    [row_minimum, row_minimum_indices] = min(mean_differences, [], 2);
    
    %Now sort this array of values.
    [~, sorted_indices] = sort(row_minimum);
    
    for i=1:m
        mapping(i,:) = [sorted_indices(i), row_minimum_indices(sorted_indices(i))];
    end
end