function V = rating_target_averaging(V, correction) 
    %Remove all entries that have a missing value in the first six columns,
    %and has a missing value in the last column.
    V(any(V(:,1:6) == -1, 2) & V(:,7) == -1,:) = [];

    for i=1:length(V)
        %If the last value is unknown, we will have to take the average of
        %the first 6 elements and use that.
        if V(i,7) == -1
            V(i,7) = mean(V(i,1:6)) - correction;
        else
            V(i,V(i,:) == -1) = V(i,7) + correction;
        end
    end
end