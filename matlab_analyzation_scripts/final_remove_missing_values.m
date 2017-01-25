%Read the table in csv form. 
T = readtable('speed_dating.csv');

%Id of the x target columns.
x_targets = [62, 63, 64, 65, 66, 67];

%Id of the y target column.
y_target = 116;

%The complete list of targets.
targets = [x_targets, y_target];

%Create the matrix.
V_cell = table2array(T(:,targets));

%Remove all occurrences of ?, and replace them with -1.
[n,m] = size(V_cell);

%Replace all question marks with -1.
for i=1:n
    for j=1:m
        if V_cell{i,j} == '?'
            V_cell{i,j} = '-1';
        end
    end
end

%Create the dataset that will work with the experimental scripts.
V = str2double(V_cell);

%Remove all rows that have only '?' in the row. Get the indices!
full_missing = all(V == -1, 2);
V(full_missing,:) = [];
T(full_missing,:) = [];
[n,m] = size(V);

%Use the missing value removal method.
V_minus_m = record_average_substitution(V);

%Put the rows back into the table.
for i=1:n
    for j=1:m
        T{i,targets(j)} = {V_minus_m(i,j)};
    end
end

%Create a new csv file.
writetable(T, 'speed_dating_altered.csv','Delimiter',',');

X = [ones(length(V_minus_m),1) V_minus_m(:,1:end-1)];
Y = V_minus_m(:,end);
beta_estimator = get_beta_vector(V_minus_m);
e = Y - X * beta_estimator;
p_s_2 = m * ((e.' * e) / (n - m));


%X_T = X.';
%X_T_X = X_T * X;
%X_T_X_inv = inv(X_T_X);

%beta_1 = X_T_X_inv * X_T * Y;
%beta_2 = X_T_X_inv * (X_T * Y);

%e_2 = Y - X * beta_2;
%p_s_2_2 = m * ((e_2.' * e_2) / (n - m));



