-- PRODUCTS TABLE
-- :name create-record! :! :1
-- :doc creates a new product record
INSERT INTO products
(asin, title, url, reviews, bsr, price, weight)
VALUES (:asin, :title, :url, :reviews, :bsr, :price, :weight)

-- :name get-asin :? :1
-- :doc retrieve asin given the product.
SELECT count(asin) FROM products
WHERE asin = :asin

-- LINKS TABLE
-- :name populate-link! :! :1
-- :doc creates a new link
INSERT INTO links
(url, used)
VALUES (:url, false)

-- :name is-url-used? :? :1
-- :doc retrieve used
SELECT used FROM links
WHERE url = :url

-- :name does-url-exist? :? :1
-- :doc retrieve used
SELECT count(url) FROM links
WHERE url = :url

-- :name get-first-url :? :1
-- :doc retrieve a sample url
SELECT url FROM links
WHERE used = false
ORDER BY url ASC
LIMIT 1

-- :name update-url-status! :? :1
-- :doc update used
UPDATE links
SET used = true
WHERE url = :url
RETURNING url

