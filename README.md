# amazon products fetcher

Retrieves information of products listed in amazon given a list of categories. Fetches the product page and parses it and gives title, price, url, best sellers rank, review score and ASIN. Can run concurrently and tested with 200 threads in a MBP. Also supports proxies and stores product information in postgres.

## Usage

* Setup a postgres database and mention the settings in db.clj
* Buy a list of proxies and mention them in getters.clj
* Thread count is managed in core.clj; Adjust it according to the machine's capabilities.

