import os

def clean_data(file_path, output_file_path):
    if not file_path:
        print("Error: File path is empty.")
        return 0  # Return 0 for error

    if not os.path.exists(file_path):
        print("Error: File not found.")
        return 0

    try:
        with open(file_path, 'r') as file:
            input_data = file.read()
    except Exception as e:
        print(f"Error: Could not read file: {e}")
        return 0

    if not input_data:
        print("Error: The file is empty.")
        return 0

    lines = input_data.strip().split('\n')
    cleaned_lines = []
    error_count = 0  # Initialize error counter

    if not lines:
        print("Error: The file contains no data.")
        return 0

    # Process the lines (edge data) - No header line processing
    for line in lines:
        parts = line.split()
        if len(parts) >= 4:
            try:
                # Convert the node and distance values to integers.
                int(parts[1])
                int(parts[2])
                int(parts[3])
                cleaned_lines.append(f"{parts[1]} {parts[2]} {parts[3]}")
            except ValueError:
                error_count += 1
                cleaned_lines.append("Error: Invalid data format.  Each data line should contain three integers.")
        elif len(parts) > 0:
            error_count += 1
            cleaned_lines.append("Error: Invalid data format.  Each data line should start with a letter and space, followed by three numbers.")
        else:
            error_count += 1
            cleaned_lines.append("Error: Empty line detected.")

    # Write the cleaned data to the output file
    try:
        with open(output_file_path, 'w') as outfile:
            outfile.write("\n".join(cleaned_lines))
    except Exception as e:
        print(f"Error: Could not write to output file: {e}")
        return error_count # Return the error count even if writing fails.

    return error_count



def main():
    file_path = input("Enter the path to the input text file: ")
    output_file_path = input("Enter the path to the output text file: ")
    error_count = clean_data(file_path, output_file_path)
    if error_count > 0:
        print(f"Errors encountered: {error_count}")
    else:
        print("Data cleaned successfully.")



if __name__ == "__main__":
    main()
