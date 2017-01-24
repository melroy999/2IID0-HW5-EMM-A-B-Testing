function V = record_maximum_substitution(V) 
    %Filter out fully unknown records.
    V(all(V == -1, 2),:) = [];

    %Get the amount of rows and columns in V.
    [n, m] = size(V);
    
    for i=1:n
        %Get the mean value of the entry, without negative values.
        record = V(i,:);
        record_max = max(record(record ~= -1));
        
        for j=1:m
            if V(i,j) == -1
                V(i,j) = record_max;
            end
        end
    end
end