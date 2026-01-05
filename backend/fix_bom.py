import os
import glob

def remove_bom(file_path):
    try:
        with open(file_path, 'rb') as f:
            content = f.read()

        # Check if file starts with UTF-8 BOM
        if content.startswith(b'\xef\xbb\xbf'):
            # Remove BOM and rewrite file
            content = content[3:]
            with open(file_path, 'wb') as f:
                f.write(content)
            print(f"Removed BOM from: {file_path}")
        else:
            print(f"No BOM found in: {file_path}")
    except Exception as e:
        print(f"Error processing {file_path}: {e}")

def main():
    # Find all Java files
    java_files = glob.glob(r'src\main\java\**\*.java', recursive=True)

    print(f"Found {len(java_files)} Java files")

    for java_file in java_files:
        remove_bom(java_file)

    print("BOM removal completed!")

if __name__ == "__main__":
    main()


















