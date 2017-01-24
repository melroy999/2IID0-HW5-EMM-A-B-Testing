function mapping = create_similarity_mapping_full(V)
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
    
    %Create an empty mapping.
    mapping = ones(m * m, 2);
    
    for i=1:m*m
        %Find the mimimum of the columns, with indices of the corresponding row.
        [min_column_values,row_indices] = min(mean_differences);
        
        %Find the minimum of the column values.
        [~,column_indices] = min(min_column_values);
        
        %Add this value to the mapping.
        mapping(i,:) = [row_indices(column_indices(1)), column_indices(1)];
        
        %Set the value to something high, so that it will not occur again.
        mean_differences(mapping(i,1),mapping(i,2)) = intmax;
    end
end