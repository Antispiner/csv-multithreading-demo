import csv
import os

def generate_files():
    with open('large-product.csv', 'w', newline='') as csvfile:
        fieldnames = ['product_id', 'product_name']
        writer = csv.DictWriter(csvfile, fieldnames=fieldnames)

        writer.writeheader()
        for i in range(1, 100001):
            writer.writerow({'product_id': i, 'product_name': f'Product Name {i}'})


    with open('large-trade.csv', 'w', newline='') as csvfile:
        fieldnames = ['date', 'product_id', 'currency', 'price']
        writer = csv.DictWriter(csvfile, fieldnames=fieldnames)

        writer.writeheader()
        for i in range(1, 1000001):
            writer.writerow({'date': '20240101', 'product_id': i % 1000001, 'currency': 'EUR', 'price': float(i % 100)})

def delete_files():
    try:
        os.remove('large-product.csv')
        os.remove('large-trade.csv')
    except OSError as e:
        print(f"Error: {e.strerror}")

if __name__ == "__main__":
    import sys
    if sys.argv[1] == "generate":
        generate_files()
    elif sys.argv[1] == "clean":
        delete_files()
