function V = ignore_missing_values(V) 
    %Get all indices of records that contain at least one missing value.
    missing_values = any(V == -1, 2);

    %Select all rows that are not present in m.
    V = V(~missing_values,:);
end