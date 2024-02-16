(function($) {
  'use strict';
  $(function() {
    if ($("#order-chart").length) {
   		var requestOptions = {
		  method: 'GET',
		  redirect: 'follow'
		};
		
		var hash = window.location.href;
		var time = "";
	    // Get the updated hash value from the URL
	    if (hash.indexOf("today") !== -1) {
		    time = "today";
		} else if (hash.indexOf("lastweek") !== -1) {
		    time = "lastweek";
		} else if (hash.indexOf("lastmonth") !== -1) {
		    time = "lastmonth";
		} else {
		    time = "today";
		}
		
		fetch("/api/v1/auth/stat/" + time, requestOptions)
		  .then(response => response.json())
		  .then(data => {
		  	var areaData = {
	        labels: data.label,
	        datasets: [
	          {
	            data: data.completed,
	            borderColor: [
	              '#2ce64b'
	            ],
	            borderWidth: 1,
	            fill: false,
	            label: "Hoàn thành"
	          },
	          {
	            data: data.canceled,
	            borderColor: [
	              '#F09397'
	            ],
	            borderWidth: 1,
	            fill: false,
	            label: "Huỷ"
	          }
	        ]
      	};
      var areaOptions = {
	    responsive: true,
	    maintainAspectRatio: true,
	    plugins: {
	      filler: {
	        propagate: false
	      }
	    },
    scales: {
      xAxes: [{
        display: true,
        ticks: {
          display: true,
          fontColor: "#6C7383"
        },
        gridLines: {
          display: false,
          drawBorder: false,
          color: 'transparent',
          zeroLineColor: '#eeeeee'
        }
      }],
      yAxes: [{
        display: true,
        ticks: {
          display: true,
          stepSize: 1, // Bước giữa các giá trị trục y là 1
		    maxTicksLimit: 5, // Giới hạn số lượng tick là 5
		    beginAtZero: true, // Bắt đầu từ 0 (nếu cần)
		    fontColor: "#6C7383",
        },
        gridLines: {
          display: true,
          color: "#f2f2f2",
          drawBorder: false
        }
      }]
    },
    legend: {
      display: true
    },
    tooltips: {
      enabled: true,
      mode: 'index', // Hiển thị tooltip cho các điểm gần nhau
      intersect: true,
    },
    elements: {
      line: {
        tension: 0.35,
        borderWidth: 5, // Điều chỉnh kích thước của line
      },
      point: {
        radius: 1,
        hoverRadius: 3,
      }
    }
};

      var revenueChartCanvas = $("#order-chart").get(0).getContext("2d");
      var revenueChart = new Chart(revenueChartCanvas, {
        type: 'line',
        data: areaData,
        options: areaOptions
      });
		  })
		  .catch(error => console.log('error', error));
      
    }

    function format ( d ) {
      // `d` is the original data object for the row
      return '<table cellpadding="5" cellspacing="0" border="0" style="width:100%;">'+
          '<tr class="expanded-row">'+
              '<td colspan="8" class="row-bg"><div><div class="d-flex justify-content-between"><div class="cell-hilighted"><div class="d-flex mb-2"><div class="mr-2 min-width-cell"><p>Policy start date</p><h6>25/04/2020</h6></div><div class="min-width-cell"><p>Policy end date</p><h6>24/04/2021</h6></div></div><div class="d-flex"><div class="mr-2 min-width-cell"><p>Sum insured</p><h5>$26,000</h5></div><div class="min-width-cell"><p>Premium</p><h5>$1200</h5></div></div></div><div class="expanded-table-normal-cell"><div class="mr-2 mb-4"><p>Quote no.</p><h6>Incs234</h6></div><div class="mr-2"><p>Vehicle Reg. No.</p><h6>KL-65-A-7004</h6></div></div><div class="expanded-table-normal-cell"><div class="mr-2 mb-4"><p>Policy number</p><h6>Incsq123456</h6></div><div class="mr-2"><p>Policy number</p><h6>Incsq123456</h6></div></div><div class="expanded-table-normal-cell"><div class="mr-2 mb-3 d-flex"><div class="highlighted-alpha"> A</div><div><p>Agent / Broker</p><h6>Abcd Enterprices</h6></div></div><div class="mr-2 d-flex"> <img src="../../images/faces/face5.jpg" alt="profile"/><div><p>Policy holder Name & ID Number</p><h6>Phillip Harris / 1234567</h6></div></div></div><div class="expanded-table-normal-cell"><div class="mr-2 mb-4"><p>Branch</p><h6>Koramangala, Bangalore</h6></div></div><div class="expanded-table-normal-cell"><div class="mr-2 mb-4"><p>Channel</p><h6>Online</h6></div></div></div></div></td>'
          '</tr>'+
      '</table>';
  }
 
$('#example tbody').on('click', 'td.details-control', function () {
  var tr = $(this).closest('tr');
  var row = table.row( tr );

  if ( row.child.isShown() ) {
      // This row is already open - close it
      row.child.hide();
      tr.removeClass('shown');
  }
  else {
      // Open this row
      row.child( format(row.data()) ).show();
      tr.addClass('shown');
  }
} );
  
  });
})(jQuery);