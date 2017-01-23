function V = attribute_average_substitution(V, mean_attribute_values) 
    for i=1:length(mean_attribute_values)
        V(V(:,i) == -1,i) = mean_attribute_values(i);
    end
end