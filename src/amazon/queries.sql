-- PRODUCTS TABLE
-- :name create-product! :! :1
-- :doc creates a new user record
INSERT INTO products
(asin, title, url, reviews, bsr, price)
VALUES (:asin, :title, :url, :reviews, :bsr, :price)

-- :name get-asin :? :1
-- :doc retrieve a user given the id.
SELECT count(asin) FROM products
WHERE asin = :asin
