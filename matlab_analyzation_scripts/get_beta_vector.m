%Get the beta vector of the data.
function beta = get_beta_vector(V)
    X = [ones(length(V),1) V(:,1:6)];
    Y = V(:,7);
    
    X_T_X = X.' * X;
    X_T_X_inv = inv(X_T_X);
    X_T_Y = X.' * Y;
    
    beta = X_T_X_inv * X_T_Y;
end