import json
import os
import re
from pathlib import Path
from typing import Any

import matplotlib.pyplot as plt
import pymysql
from matplotlib.ticker import MultipleLocator
from pymysql.cursors import DictCursor


# 表名不能使用 SQL 参数占位符，因此必须先严格校验。
TABLE_NAME_PATTERN = re.compile(r"^[A-Za-z_][A-Za-z0-9_]*$")


def get_connection() -> pymysql.Connection:
    """根据环境变量创建 MySQL 数据库连接。"""
    return pymysql.connect(
        host=os.getenv("DB_HOST", "10.168.1.200"),
        port=int(os.getenv("DB_PORT", "3306")),
        user=os.getenv("DB_USER", "admin"),
        password=os.getenv("DB_PASSWORD", "admin@2017!@#"),
        database=os.getenv("DB_NAME", "battery_monitoring_system"),
        charset="utf8",
        cursorclass=DictCursor,
    )


def read_device_numbers(file_path: str | Path) -> list[str]:
    """读取逗号或换行分隔的设备编号，并保持原顺序去重。"""
    content = Path(file_path).read_text(encoding="utf-8-sig")
    device_numbers = [item.strip() for item in re.split(r"[,\r\n]+", content)]
    return list(dict.fromkeys(item for item in device_numbers if item))


def query_latest_by_devices(
    table_name: str, device_numbers: list[str]
) -> list[dict[str, Any]]:
    """查询每个指定设备创建时间最新的一条数据。"""
    if not TABLE_NAME_PATTERN.fullmatch(table_name):
        raise ValueError(f"非法表名: {table_name!r}")
    if not device_numbers:
        return []

    sql = (
        f"SELECT `dev_no`, `zRealOhm`, `zImagOhm`, `create_time` "
        f"FROM `{table_name}` WHERE `dev_no` = %s "
        f"AND `create_time` = (SELECT MAX(`create_time`) FROM `{table_name}` "
        "WHERE `dev_no` = %s) LIMIT 1"
    )
    connection = get_connection()
    try:
        with connection.cursor() as cursor:
            rows = []
            for device_no in device_numbers:
                cursor.execute(sql, (device_no, device_no))
                row = cursor.fetchone()
                if row is None:
                    print(f"设备 {device_no} 未查询到数据")
                    continue
                rows.append(row)
            return rows
    finally:
        connection.close()


def parse_json_numbers(value: str | list[Any] | None) -> list[float]:
    """将数据库 JSON 字段转换为浮点数列表。"""
    if value is None:
        return []
    values = json.loads(value) if isinstance(value, str) else value
    return [float(item) for item in values]


def plot_impedance(rows: list[dict[str, Any]]) -> None:
    """每条记录单独绘图，坐标数值以 10^-4 Ω 为单位显示。"""
    scale = 10_000
    plotted_count = 0
    output_dir = "impedance_plots"
    os.makedirs(output_dir, exist_ok=True)

    for index, row in enumerate(rows, start=1):
        x_values = parse_json_numbers(row.get("zRealOhm"))
        y_values = parse_json_numbers(row.get("zImagOhm"))
        if not x_values or not y_values:
            continue
        if len(x_values) != len(y_values):
            print(f"第 {index} 条数据长度不一致，已跳过")
            continue

        device_no = str(row.get("dev_no") or "unknown")
        create_time = row.get("create_time")
        time_text = create_time.strftime("%Y-%m-%d %H:%M:%S") if create_time else "unknown"
        safe_device_no = re.sub(r"[^A-Za-z0-9_-]", "_", device_no)
        figure, axes = plt.subplots(figsize=(8, 6))
        axes.plot(
            [value * scale for value in x_values],
            [-value * scale for value in y_values],
            marker="o",
            markersize=4,
        )
        axes.set_title(
            f"Device No: {device_no}\nCreate Time: {time_text}",
            fontsize=14,
            fontweight="bold",
        )
        axes.set_xlabel("zRealOhm (10⁻⁴ Ω)")
        axes.set_ylabel("-zImagOhm (10⁻⁴ Ω)")
        axes.xaxis.set_major_locator(MultipleLocator(0.5))
        axes.yaxis.set_major_locator(MultipleLocator(0.5))
        axes.grid(True, alpha=0.3)
        figure.tight_layout()

        output_file = os.path.join(
            output_dir, f"{index:02d}_{safe_device_no}.png"
        )
        figure.savefig(output_file, dpi=200)
        print(f"已生成设备 {device_no} 的图：{output_file}")
        plotted_count += 1

    if plotted_count == 0:
        print("没有可绘制的阻抗数据")
        return

    plt.show()


if __name__ == "__main__":
    table_name = os.getenv("DB_TABLE", "xianjiaoda_battery202608")
    device_file = Path(__file__).with_name("dev_no.txt")
    device_numbers = read_device_numbers(device_file)
    rows = query_latest_by_devices(table_name, device_numbers)

    print(f"读取到 {len(device_numbers)} 个设备编号，查询到 {len(rows)} 条最新数据")
    plot_impedance(rows)
