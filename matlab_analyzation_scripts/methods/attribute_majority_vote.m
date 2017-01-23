function V = attribute_majority_vote(V, mean_attribute_mode) 
    for i=1:length(mean_attribute_mode)
        V(V(:,i) == -1,i) = mean_attribute_mode(i);
    end
end