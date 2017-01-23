function V = record_maximum_substitution(V) 
    %Filter out fully unknown records.
    V(all(V == -1, 2),:) = [];

    for i=1:length(V)
        %Get the mean value of the entry, without negative values.
        record = V(i,:);
        record_max = max(record(record ~= -1));
        
        for j=1:7
            if V(i,j) == -1
                V(i,j) = record_max;
            end
        end
    end
end