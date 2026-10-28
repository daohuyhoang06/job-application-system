import json
import glob
import os
import sys
import statistics

# Đảm bảo in tiếng Việt chuẩn trên Windows
if sys.platform == "win32":
    try:
        sys.stdout.reconfigure(encoding='utf-8')
    except Exception:
        pass

def parse_run(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        data = json.load(f)
    metrics = data.get('metrics', {})
    
    # 1. Duration metrics
    duration = metrics.get('http_req_duration', {})
    if 'values' in duration:
        duration = duration['values']
        
    avg_ms = duration.get('avg', 0.0)
    p90_ms = duration.get('p(90)', 0.0)
    p95_ms = duration.get('p(95)', 0.0)
    p99_ms = duration.get('p(99)', duration.get('max', 0.0))
    max_ms = duration.get('max', 0.0)
    med_ms = duration.get('med', 0.0)
    
    # 2. HTTP Requests (RPS & Count)
    reqs = metrics.get('http_reqs', {})
    if 'values' in reqs:
        reqs = reqs['values']
    count = reqs.get('count', 0)
    rate = reqs.get('rate', 0.0)
    
    # 3. Failed requests
    failed = metrics.get('http_req_failed', {})
    if 'values' in failed:
        failed = failed['values']
    passes = failed.get('passes', 0)
    fails = failed.get('fails', 0)
    total_checks = passes + fails
    
    if total_checks > 0:
        error_rate_pct = (passes / total_checks) * 100.0
    else:
        error_rate_pct = failed.get('value', 0.0) * 100.0

    return {
        'file': os.path.basename(file_path),
        'total_requests': count,
        'rps': rate,
        'avg_ms': avg_ms,
        'med_ms': med_ms,
        'p90_ms': p90_ms,
        'p95_ms': p95_ms,
        'p99_ms': p99_ms,
        'max_ms': max_ms,
        'error_rate_pct': error_rate_pct
    }

def summarize_scenario(scenario_folder, title, default_sla_p95=1500.0, default_sla_error=5.0):
    json_files = sorted(glob.glob(os.path.join(scenario_folder, '*.json')))
    if not json_files:
        print(f"\n[!] Thư mục {scenario_folder} hiện chưa có file JSON nào.")
        return None
        
    runs = [parse_run(f) for f in json_files]
    
    print("\n" + "=" * 106)
    print(f"        TỔNG HỢP KẾT QUẢ: {title}")
    print("=" * 106)
    print(f"{'Lần chạy':<26} | {'Tổng Reqs':<10} | {'Throughput (RPS)':<18} | {'Avg (ms)':<9} | {'P95 (ms)':<9} | {'Max (ms)':<9} | {'Lỗi (%)'}")
    print("-" * 106)
    
    for r in runs:
        print(f"{r['file']:<26} | {r['total_requests']:<10} | {r['rps']:<18.2f} | {r['avg_ms']:<9.2f} | {r['p95_ms']:<9.2f} | {r['max_ms']:<9.2f} | {r['error_rate_pct']:<7.2f}%")
        
    print("-" * 106)
    
    avg_rps = statistics.mean([r['rps'] for r in runs])
    avg_duration = statistics.mean([r['avg_ms'] for r in runs])
    avg_med = statistics.mean([r['med_ms'] for r in runs])
    avg_p90 = statistics.mean([r['p90_ms'] for r in runs])
    avg_p95 = statistics.mean([r['p95_ms'] for r in runs])
    avg_max = statistics.mean([r['max_ms'] for r in runs])
    avg_error = statistics.mean([r['error_rate_pct'] for r in runs])
    total_reqs_mean = int(statistics.mean([r['total_requests'] for r in runs]))
    
    print(f"{'TRUNG BÌNH (AVERAGE)':<26} | {total_reqs_mean:<10} | {avg_rps:<18.2f} | {avg_duration:<9.2f} | {avg_p95:<9.2f} | {avg_max:<9.2f} | {avg_error:<7.2f}%")
    print("=" * 106)
    
    pass_sla = (avg_error < default_sla_error) and (avg_p95 < default_sla_p95)
    sla_status = "✓ ĐẠT (PASS)" if pass_sla else "✗ CẢNH BÁO / KHÔNG ĐẠT"
    print(f"🎯 Đánh giá SLA (Error < {default_sla_error}% & P95 < {default_sla_p95}ms): {sla_status}")
    print(f"   • Thực tế: Lỗi = {avg_error:.2f}% | P95 = {avg_p95:.2f} ms")
    print("\n👉 BẢNG SỐ LIỆU ĐƯA VÀO BÁO CÁO BTL:")
    print(f" • Tổng số request xử lý TB : {total_reqs_mean:,} reqs")
    print(f" • Throughput trung bình    : {avg_rps:.2f} reqs/sec")
    print(f" • Thời gian phản hồi TB    : {avg_duration:.2f} ms")
    print(f" • Thời gian trung vị (med) : {avg_med:.2f} ms")
    print(f" • Độ trễ P90               : {avg_p90:.2f} ms")
    print(f" • Độ trễ P95               : {avg_p95:.2f} ms")
    print(f" • Độ trễ tối đa (Max)      : {avg_max:.2f} ms")
    print(f" • Tỷ lệ lỗi (Error Rate)   : {avg_error:.2f}%\n")
    
    return {
        'title': title,
        'total_reqs': total_reqs_mean,
        'rps': avg_rps,
        'avg_ms': avg_duration,
        'med_ms': avg_med,
        'p90_ms': avg_p90,
        'p95_ms': avg_p95,
        'max_ms': avg_max,
        'error_rate_pct': avg_error
    }

def main():
    base_dir = os.path.dirname(__file__)
    phase1_dir = os.path.join(base_dir, 'phase1')
    
    scen1_dir = os.path.join(phase1_dir, 'scenario1')
    scen2_dir = os.path.join(phase1_dir, 'scenario2')
    scen3_dir = os.path.join(phase1_dir, 'scenario3')
    scen4_dir = os.path.join(phase1_dir, 'scenario4')
    scen5_dir = os.path.join(phase1_dir, 'scenario5')
    
    # Xác định kịch bản cần hiển thị từ tham số dòng lệnh hoặc nhập từ bàn phím
    choice = "all"
    if len(sys.argv) > 1:
        choice = sys.argv[1].strip().lower()
    elif sys.stdin.isatty():
        print("=" * 60)
        print(" CHỌN KỊCH BẢN KIỂM THỬ CẦN XEM BÁO CÁO:")
        print("  [1]  : Kịch bản 1 (Baseline Read - 50 VUs)")
        print("  [2]  : Kịch bản 2 (Stress Test - 100 -> 300 VUs)")
        print("  [3]  : Kịch bản 3 (Auth & Write Lifecycle)")
        print("  [4]  : Kịch bản 4 (Spike Test - 1.000 VUs & Recovery)")
        print("  [5]  : Kịch bản 5 (High-Throughput - 1.000 -> 2.000 RPS)")
        print("  [all]: Xem tất cả kịch bản + Bảng so sánh đối chiếu")
        print("=" * 60)
        try:
            user_input = input("Nhập số kịch bản bạn muốn xem (1/2/3/4/5/all) [Mặc định: all]: ").strip()
            if user_input:
                choice = user_input.lower()
        except EOFError:
            choice = "all"

    # Xử lý theo lựa chọn
    if choice in ['1', 'scen1', 'scenario1']:
        summarize_scenario(scen1_dir, "KỊCH BẢN 1: BASELINE CAPACITY (50 VUs)", default_sla_p95=1500.0)
    elif choice in ['2', 'scen2', 'scenario2']:
        summarize_scenario(scen2_dir, "KỊCH BẢN 2: STRESS TEST (100 -> 300 VUs)", default_sla_p95=3000.0)
    elif choice in ['3', 'scen3', 'scenario3']:
        summarize_scenario(scen3_dir, "KỊCH BẢN 3: AUTH & WRITE OPERATIONS", default_sla_p95=2500.0)
    elif choice in ['4', 'scen4', 'scenario4']:
        summarize_scenario(scen4_dir, "KỊCH BẢN 4: SPIKE TEST (1.000 VUs & PHỤC HỒI)", default_sla_p95=3000.0, default_sla_error=15.0)
    elif choice in ['5', 'scen5', 'scenario5']:
        summarize_scenario(scen5_dir, "KỊCH BẢN 5: HIGH-THROUGHPUT (1.000 -> 2.000 RPS)", default_sla_p95=2000.0, default_sla_error=10.0)
    else:
        # Mặc định: Hiển thị tất cả và đối chiếu
        res1 = summarize_scenario(scen1_dir, "KỊCH BẢN 1: BASELINE CAPACITY (50 VUs)", default_sla_p95=1500.0)
        res2 = summarize_scenario(scen2_dir, "KỊCH BẢN 2: STRESS TEST (100 -> 300 VUs)", default_sla_p95=3000.0)
        res3 = summarize_scenario(scen3_dir, "KỊCH BẢN 3: AUTH & WRITE OPERATIONS", default_sla_p95=2500.0)
        res4 = summarize_scenario(scen4_dir, "KỊCH BẢN 4: SPIKE TEST (1.000 VUs & PHỤC HỒI)", default_sla_p95=3000.0, default_sla_error=15.0)
        res5 = summarize_scenario(scen5_dir, "KỊCH BẢN 5: HIGH-THROUGHPUT (1.000 -> 2.000 RPS)", default_sla_p95=2000.0, default_sla_error=10.0)
        
        results = [r for r in [res1, res2, res3, res4, res5] if r is not None]
        if len(results) >= 2:
            print("\n" + "#" * 106)
            print("                 BẢNG ĐỐI CHIẾU CÁC KỊCH BẢN KIỂM THỬ TẢI PHA 1 (DÀNH CHO BÁO CÁO BTL)")
            print("#" * 106)
            print(f"{'Kịch bản kiểm thử':<40} | {'Throughput (RPS)':<18} | {'Avg Latency':<14} | {'P95 Latency':<14} | {'Max Latency'}")
            print("-" * 106)
            for r in results:
                print(f"{r['title']:<40} | {r['rps']:<18.2f} | {r['avg_ms']:<11.2f} ms | {r['p95_ms']:<11.2f} ms | {r['max_ms']:<11.2f} ms")
            print("#" * 106)
            
            if res1 and res2:
                rps_increase = (res2['rps'] / res1['rps']) if res1['rps'] > 0 else 0
                lat_increase = (res2['avg_ms'] / res1['avg_ms']) if res1['avg_ms'] > 0 else 0
                p95_increase = (res2['p95_ms'] / res1['p95_ms']) if res1['p95_ms'] > 0 else 0
                print("\n💡 NHẬN XÉT ĐẶC BIỆT DÀNH CHO MỤC PHÂN TÍCH KIẾN TRÚC:")
                print(f" 1. Khi tăng tải từ 50 VUs lên 300 VUs (Kịch bản 2):")
                print(f"    - Thông lượng (RPS) tăng {rps_increase:.1f} lần (từ {res1['rps']:.1f} lên {res2['rps']:.1f} reqs/s).")
                print(f"    - TUY NHIÊN, Độ trễ trung bình tăng vọt tới {lat_increase:.1f} LẦN (từ {res1['avg_ms']:.2f}ms lên {res2['avg_ms']:.2f}ms)!")
                print(f"    - Độ trễ P95 tăng {p95_increase:.1f} LẦN (từ {res1['p95_ms']:.2f}ms lên {res2['p95_ms']:.2f}ms), và trễ đỉnh điểm đạt {res2['max_ms']:.2f}ms.")
                print(" 2. Kết luận: Hệ thống bắt đầu xuất hiện hiện tượng 'Hàng đợi tích lũy' (Queueing Contention) tại")
                print("    Hikari Connection Pool và MySQL Disk I/O. Đây chính là minh chứng rõ ràng nhất để")
                print("    đề xuất TẦNG BỘ ĐỆM CACHE (Redis/Caffeine) ở Pha 2 nhằm giải phóng I/O cho Database!\n")

if __name__ == '__main__':
    main()
