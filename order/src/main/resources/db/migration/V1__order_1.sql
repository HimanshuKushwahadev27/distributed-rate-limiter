CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    price_paid INT NOT NULL,
    quantity INT NOT NULL,
    sku_code VARCHAR(255) NOT NULL
);

CREATE TABLE inventory (
    id SERIAL PRIMARY KEY,
    sku_code VARCHAR(255) NOT NULL,
    quantity INT NOT NULL
);