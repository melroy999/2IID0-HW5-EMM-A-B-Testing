%Mapping is a m x 2 matrix where the first column denotes the value we want
%a replacement for, and the second column is the position where we can get
%this candidate value. The rows of this matrix are sorted on lowest mean
%difference.
function V = attribute_similarity(V, mapping) 
    %Filter out fully unknown records.
    V(all(V == -1, 2),:) = [];

    %Get the amount of rows and columns in V.
    [n, m] = size(V);
    
    for i=1:n
        %We have to iterate multiple times over the replacement list, as we
        %might have transititivity in the table. We break the inner loop
        %whenever a value is replaced, to be sure that we always pick the
        %best options.
        for j=1:length(mapping)
            for k=1:length(mapping)
                replacement = mapping(k,:);
                
                %We only want to replace when the source value is minus
                %one, so skip otherwise.
                if V(i,replacement(1)) == -1 
                    %If the replacement value is not empty, we can replace!
                    if V(i,replacement(2)) ~= -1 
                        V(i,replacement(1)) = V(i,replacement(2));
                        
                        %We found a replacement, break the inner loop.
                        break
                    end
                end
            end
        end
    end
    
    %Remove all entries that still have a missing value.
    %V = record_average_substitution(V);
    V(any(V == -1, 2),:) = [];
end