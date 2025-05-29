import os
import math
import matplotlib.pyplot as plt
from datetime import datetime
from pathlib import Path
import numpy as np
import logging
import csv
import pandas as pd


def setup_logger(logger_name="tool_test"):
    """Set up logging for tests."""
    logger = logging.getLogger(logger_name)
    if logger.hasHandlers():
        logger.handlers.clear()

    logger.setLevel(logging.INFO)

    console_handler = logging.StreamHandler()
    console_handler.setLevel(logging.INFO)
    console_formatter = logging.Formatter('%(message)s')
    console_handler.setFormatter(console_formatter)
    logger.addHandler(console_handler)

    log_dir = Path("logs")
    log_dir.mkdir(exist_ok=True)
    file_handler = logging.FileHandler(log_dir / f"{logger_name}.log")
    file_handler.setLevel(logging.INFO)
    file_formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
    file_handler.setFormatter(file_formatter)
    logger.addHandler(file_handler)

    return logger


def add_metric(
    model: str,
    query_id: str,
    status: str,
    tool_call_match: bool,
    inference_not_empty: bool,
    expected_tool_call: str = "N/A",
    error: str = ""
):
    """Add a metric record to the CSV file."""
    results_dir = Path("results")
    results_dir.mkdir(exist_ok=True)

    metrics_file = results_dir / "metrics.csv"

    if not metrics_file.exists():
        with open(metrics_file, 'w', newline='') as f:
            writer = csv.writer(f)
            writer.writerow([
                'timestamp',
                'model',
                'query_id',
                'status',
                'tool_call_match',
                'inference_not_empty',
                'expected_tool_call',
                'error'
            ])

    with open(metrics_file, 'a', newline='') as f:
        writer = csv.writer(f)
        writer.writerow([
            datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
            model,
            query_id,
            status,
            tool_call_match,
            inference_not_empty,
            expected_tool_call,
            error
        ])


def get_subplot_grid(n):
    """ Calculate the number of rows and columns for subplots based on the number of plots. """
    cols = math.ceil(math.sqrt(n))
    rows = math.ceil(n / cols)
    return rows, cols


def save_plot(fig, filename, dpi=300, bbox_inches='tight'):
    """
    Save a matplotlib figure as a JPEG file inside the 'results/' folder.
    """
    results_dir = 'results/plots/'
    os.makedirs(results_dir, exist_ok=True)

    if not filename.lower().endswith(('.jpg', '.jpeg')):
        filename += '.jpg'
    filename = filename.replace(" ", "_")

    full_path = os.path.join(results_dir, filename)
    file_directory = os.path.dirname(full_path)
    os.makedirs(file_directory, exist_ok=True)

    fig.savefig(full_path, format='jpeg', dpi=dpi, bbox_inches=bbox_inches)
    print(f"Plot saved as JPEG: '{full_path}'")


def add_plot(fig, ax, df, column_name, title, save_filename=True):
    """ Add a stacked bar chart to the given axes."""
    # Calculate true counts, false counts, and total counts per model
    models = df['model'].unique().tolist()
    true_counts = df[df[column_name] == True].groupby('model')[column_name].count().reindex(models, fill_value=0)
    false_counts = df[df[column_name] == False].groupby('model')[column_name].count().reindex(models, fill_value=0)
    models = true_counts.index.tolist()
    true_vals = true_counts.values
    false_vals = false_counts.values
    x = np.arange(len(models))
    # Create stacked bar chart
    bars_true = ax.bar(x, true_vals, label='True', color='mediumseagreen')
    bars_false = ax.bar(x, false_vals, bottom=true_vals, label='False', color='lightgray')
    # Add text labels (true - outside top)
    for bar in bars_true:
        height = bar.get_height()
        if height > 0:
            ax.text(bar.get_x() + bar.get_width() / 2, height - 1, int(height),
                    ha='center', va='bottom', fontsize=8, color='white')
    # Add text labels (false - inside middle)
    for i, bar in enumerate(bars_false):
        height = bar.get_height()
        if height > 0:
            ax.text(bar.get_x() + bar.get_width() / 2, true_vals[i] + height / 2, int(height),
                    ha='center', va='center', fontsize=8, color='black')
    # Customize axes and layout
    ax.set_xticks(x)
    ax.set_xticklabels(models, rotation=45, ha='right')
    ax.set_ylabel('True/False Count')
    ax.set_title(title)
    ax.legend(loc='upper right')

    if save_filename:
        save_plot(fig, title)


def add_per_tool_plot(df, column_name, title):
    """
    Creates a stacked bar chart showing True/False counts for a given column, grouped by 'expected_tool_call'.
    """
    df_filtered = df[df['expected_tool_call'] != 'N/A']
    if df_filtered.empty:
        print(f"No data available for plotting '{title}' per tool.")
        return

    tool_names = df_filtered['expected_tool_call'].unique().tolist()
    tool_names.sort()

    true_counts = df_filtered[df_filtered[column_name] == True].groupby('expected_tool_call')[column_name].count().reindex(tool_names, fill_value=0)
    false_counts = df_filtered[df_filtered[column_name] == False].groupby('expected_tool_call')[column_name].count().reindex(tool_names, fill_value=0)

    tools = true_counts.index.tolist()
    true_vals = true_counts.values
    false_vals = false_counts.values
    x = np.arange(len(tools))

    fig, ax = plt.subplots(figsize=(max(10, len(tools) * 0.8), 6))

    bars_true = ax.bar(x, true_vals, label='True', color='mediumseagreen')
    bars_false = ax.bar(x, false_vals, bottom=true_vals, label='False', color='lightgray')

    for bar in bars_true:
        height = bar.get_height()
        if height > 0:
            ax.text(bar.get_x() + bar.get_width() / 2, height - 1, int(height),
                    ha='center', va='bottom', fontsize=8, color='white')

    for i, bar in enumerate(bars_false):
        height = bar.get_height()
        if height > 0:
            ax.text(bar.get_x() + bar.get_width() / 2, true_vals[i] + height / 2, int(height),
                    ha='center', va='center', fontsize=8, color='black')

    ax.set_xticks(x)
    ax.set_xticklabels(tools, rotation=90, ha='right', fontsize=9)
    ax.set_ylabel('Count')
    ax.set_title(title)
    ax.legend(loc='upper right')

    plt.tight_layout()
    plt.show()
    save_plot(fig, title)


def get_analysis_plots():
    file_path = './results/metrics.csv'
    try:
        df = pd.read_csv(file_path)
    except FileNotFoundError:
        print(f"Metrics file not found at {file_path}. Cannot generate plots.")
        return
    except pd.errors.EmptyDataError:
        print(f"Metrics file at {file_path} is empty. Cannot generate plots.")
        return

    print(f"\n=== Generating plots ===")

    add_per_tool_plot(df, column_name='tool_call_match', title='Tool Call Match Per Function/Tool')
    add_per_tool_plot(df, column_name='inference_not_empty', title='Inference Not Empty Per Function/Tool')

    fig, ax = plt.subplots(figsize=(8, 6))
    add_plot(fig, ax, df, column_name='tool_call_match', title='Overall comparison check of correct tool call')
    plt.tight_layout()
    plt.show()

    fig, ax = plt.subplots(figsize=(8, 6))
    add_plot(fig, ax, df, column_name='inference_not_empty', title='Overall comparison check of inference not empty')
    plt.tight_layout()
    plt.show()
