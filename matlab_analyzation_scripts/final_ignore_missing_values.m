% ====== Variables that can be altered.

%The name of the input CSV file, which is the file saved through Weka.
input_file_name = 'speed_dating.csv'; 

%The name of the output CSV file, which is the file in which all missing values have been processed.
output_file_name = 'speed_dating_altered_important.csv'; 

%Id of the x target columns.
%The indices of the attributes that should be considered the x-targets of the regression model, where the attribute with index 1 is the first attribute in the dataset.
%x_targets = [62, 63, 64, 65, 66, 67]; 
x_targets = [40, 41, 42, 43, 44, 45]; 

%Id of the y target column.
%The index of the attribute that should be considered the y-target of the regression model, where the attribute with index 1 is the first attribute in the dataset.
y_target = 116; 
%y_target = 117; 

% ====== End of variables that can be altered.




%Read the table in csv form. 
T = readtable(input_file_name);

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
has_missing = any(V == -1, 2);
T(has_missing,:) = [];

%Create a new csv file.
writetable(T, output_file_name,'Delimiter',',');



