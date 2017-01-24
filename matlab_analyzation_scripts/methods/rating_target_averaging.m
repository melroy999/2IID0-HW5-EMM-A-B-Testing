function V = rating_target_averaging(V) 
    %Remove all entries that have a missing value in the first six columns,
    %and has a missing value in the last column.
    V(any(V(:,1:end-1) == -1, 2) & V(:,end) == -1,:) = [];

    for i=1:length(V)
        %If the last value is unknown, we will have to take the average of
        %the first end-1 elements and use that.
        if V(i,end) == -1
            V(i,end) = mean(V(i,1:end-1));
        else
            V(i,V(i,:) == -1) = V(i,end);
        end
    end
end