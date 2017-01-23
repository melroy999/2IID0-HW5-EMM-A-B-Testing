function V = record_minimum_substitution(V) 
    %Filter out fully unknown records.
    V(all(V == -1, 2),:) = [];

    for i=1:length(V)
        %Get the mean value of the entry, without negative values.
        record = V(i,:);
        record_min = min(record(record ~= -1));
        
        for j=1:7
            if V(i,j) == -1
                V(i,j) = record_min;
            end
        end
    end
end