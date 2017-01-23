values = csvread('speed_dating_numeric_targets.csv', 1, 0);

centers = -1:0.5:10;
variable_names = {'attractive\_partner' 'sincere\_partner' 'intelligence\_partner' 'funny\_partner' 'ambition\_partner' 'shared\_interests\_partner' 'like'};

for i=1:7
    column = values(:,i);
    column(column < 0) = [];
    mean_value = mean(column);
    std_value = std(column);
     
    set(gcf,'position',[0,0,1000,1414]);
    if i < 7
        sub_plot = subplot(4,2,i);
    else 
        sub_plot = subplot(4,2,[i i+1]);
    end
    
    set(gcf,'units','pixel');
    
    counts = hist(values(:,i), centers);
    bar(centers, counts);
    
    p = get(sub_plot, 'pos');
    if i == 7
        p = p + [0.20 0 -0.4 -0.025];
    else 
        p = p + [0 0 0 -0.025];
    end
    set(sub_plot, 'pos', p);
        
    xlim([-1.5 10.5]);
    ylim([0 2500]);
    set(gca,'XTick', -1:1:10)
    xlabel(['Value (mean = ', num2str(mean_value), ', std = ', num2str(std_value), ')'])
    ylabel('Value frequency');
    title(['Attribute "', variable_names{i}, '"']);
    
    if i == 7
        suptitle('Frequency bar plots for selected attributes')
        print('frequency_figures','-dpng','-r300')
    end
end

%Values that have no missing entries.
well_defined_values = values(~any(values == -1, 2),:);
sum_of_ratings = sum(well_defined_values(:,(1:6)), 2);
average_of_ratings = mean(well_defined_values(:,(1:6)), 2);

%Take the difference between the average of the ratings and the
%corresponding like rating.
rating_difference = average_of_ratings - well_defined_values(:,7);
rating_counts = tabulate(rating_difference);

figure;
set(gcf,'position',[0,0,960,500]);
normplot(rating_difference);
title('Average - target rating difference normal plot');
xlabel(['Difference between rate average and target rate (mean = ' num2str(mean(rating_difference)) ', std = ' num2str(std(rating_difference)) ')']);
print('difference_normal_plot','-dpng','-r300')

undefined_values = double(logical(values == -1));
undefined_values_sum = sum(undefined_values,2);

figure;
set(gcf,'units','pixel');
set(gcf,'position',[0,0,960,300]);

counts = hist(undefined_values_sum, 0:7);
bar(0:7, counts);
text(0:7,counts',num2str(counts','%0.2f'),... 
'HorizontalAlignment','center',... 
'VerticalAlignment','bottom')

title('Frequency of missing values in single records');
ylabel('Value frequency');
xlabel(['Amount of undefined values in entries (mean = ' num2str(mean(undefined_values_sum)) ', std = ' num2str(std(undefined_values_sum)) ')']);

print('total_missing_values','-dpng','-r300')

undefined_target_values_sum = sum(undefined_values(:,7));

%Part 2, which uses well_defined_values again.
%For each column combination of the six rankings, compare the values.
mean_differences = zeros(7);
std_differences = zeros(7);
for i=1:7
    for j=1:7
        mean_differences(i,j) = mean(well_defined_values(:,i) - well_defined_values(:,j));
        std_differences(i,j) = std(well_defined_values(:,i) - well_defined_values(:,j));
    end
end

%most_occurring_record_value = mode(well_defined_values(:,(1:6)), 2);
%1:end ~= k
most_occurring_difference = zeros(length(well_defined_values), 7);
mean_of_entry_difference = zeros(length(well_defined_values), 7);
mean_of_entry_difference_rounded = zeros(length(well_defined_values), 7);
for i=1:length(well_defined_values)
    for j=1:7
        most_occurring_value = mode(well_defined_values(i, 1:7 ~= j), 2);
        most_occurring_difference(i, j) = abs(most_occurring_value - well_defined_values(i,j));
        
        mean_of_entry = mean(well_defined_values(i, 1:7 ~= j), 2);
        mean_of_entry_difference_rounded(i, j) = abs(round(mean_of_entry) - well_defined_values(i,j));
        mean_of_entry_difference(i, j) = abs(mean_of_entry - well_defined_values(i,j));
        
    end
end

number_of_most_occurring_matches = sum(sum(most_occurring_difference == 0));
number_of_mean_of_entry_matches = sum(sum(mean_of_entry_difference_rounded == 0));

mean2(mean_of_entry_difference)
std2(mean_of_entry_difference)












