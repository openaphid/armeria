class Result:
    def __init__(self, c, t):
        self.cfg_num_connection = c
        self.cfg_num_thread = t

    def __repr__(self):
        return str(self.__dict__)


def parse_h2load_log(log_file):
    import re
    all_results = []
    current = None

    def _parse_time(num, metric):
        num = float(num)
        if metric == 'us':
            return num
        elif metric == 'ms':
            return num * 1000
        elif metric == 's':
            return num * 1000 * 1000
        print(f'unknown metric: {metric}')

    for line in open(log_file):
        if line.startswith('h2load'):
            m = re.search(r'h2load -c ([0-9]+) -t ([0-9]+)', line)
            current = Result(c=m.group(1), t=m.group(2))
            all_results.append(current)
        elif line.startswith('finished in'):
            m = re.search(r'finished in (.*)s, (.*) req/s', line)
            current.total_time = float(m.group(1))
            current.total_req_per_sec = float(m.group(2))
        elif line.startswith('traffic:'):
            m = re.search(
                r'traffic: .*B \((.*)\) total, .*B \((.*)\) headers \(space savings.*\), .*B \((.*)\) data',
                line)
            current.traffic_total = int(m.group(1))
            current.traffic_headers = int(m.group(2))
            current.traffic_data = int(m.group(3))
        elif line.startswith('time for request'):
            m = re.search(
                r'time for request:[ ]*([0-9|.]+)([us|ms|s]+)[ ]*([0-9|.]+)([us|ms|s]+)[ ]*([0-9|.]+)([us|ms|s]+)',
                line)
            current.time_req_min = _parse_time(m.group(1), m.group(2))
            current.time_req_max = _parse_time(m.group(3), m.group(4))
            current.time_req_mean = _parse_time(m.group(5), m.group(6))
        elif line.startswith('req/s'):
            m = re.search(r'req/s *: *([0-9|.]+) *([0-9|.]+) *([0-9|.]+) *',
                          line)
            current.req_per_sec_per_conn_min = float(m.group(1))
            current.req_per_sec_per_conn_max = float(m.group(2))
            current.req_per_sec_per_conn_mean = float(m.group(3))
    return all_results


def plot_results(results_tuples, vs, normalize=False):
    import pandas as pd
    from matplotlib import pyplot as plt
    import seaborn as sns

    sns.set_context("paper",
                    rc={
                        "font.size": 16,
                        "axes.titlesize": 16,
                        "axes.labelsize": 12
                    })

    data = []
    
    tuple_baseline = results_tuples[0] # ('NAME', [Result])
    
    for i in range(0, len(tuple_baseline[1])):
        r1 = tuple_baseline[1][i]
        data.append([
            f't{r1.cfg_num_thread}/c{r1.cfg_num_connection}',
            tuple_baseline[0],
            r1.__dict__[vs],
            1.0
        ])
        
        for j in range(1, len(results_tuples)):
            tuple_control = results_tuples[j]
            r2 = tuple_control[1][i]
            data.append([
                f't{r2.cfg_num_thread}/c{r2.cfg_num_connection}',
                tuple_control[0],
                r2.__dict__[vs],
                r2.__dict__[vs] / r1.__dict__[vs]
            ])
    df = pd.DataFrame(data, columns=['h2load', 'framework', vs, 'normalized'])
    plt.figure(figsize=(16, 5))
    sns.barplot(
        data=df,
        x='h2load',
        y='normalized' if normalize else vs,
        hue='framework').set_title(vs if not normalize else vs + ' (normalized)')
    plt.show()
    return df