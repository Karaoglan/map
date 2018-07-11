import {Component, OnInit} from '@angular/core';
import {DemoService} from "./app.service";

import { } from '@types/googlemaps';
import {ModalDismissReasons, NgbModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
  title = 'OpenStreetMap Validator';
  data: any = {};

  statisticData: any = {};

  fileToUpload: File = null;
  isFileUploaded: boolean = false;
//[hidden]="isFileUploaded"

  closeResult: string;

  //TODO statistic mock values
  numberOfGooglePlaces;
  numberOfOpenPlaces;
  numberOfTrueOpen;

  width = 600;
  height = 400;
  type = 'pie3d';
  dataFormat = 'json';
  dataSource = {
    "chart": {
      "caption": "Openstreetmap places names mapping",
      "subcaption": "Comparing to Google Maps Places",
      "startingangle": "120",
      "showlabels": "0",
      "showlegend": "1",
      "enablemultislicing": "0",
      "slicingdistance": "15",
      "showpercentvalues": "1",
      "showpercentintooltip": "0",
      "plottooltext": "Age group : $label Total visit : $datavalue",
      "theme": "ocean"
    },
    "data": [
      {
        "label": "OpenstreetTrue",
        "value": this.numberOfTrueOpen,
        "color": "#3ADF00"
      },
      {
        "label": "OpenstreetFalse",
        "value": this.numberOfOpenPlaces - this.numberOfTrueOpen,
        "color": "#FF0000"
      },
      {
        "label": "OpenstreetNotmapped",
        "value": this.numberOfGooglePlaces - this.numberOfOpenPlaces,
        "color": "#A9F5F2"
      }
    ]
  };

  constructor(private demoService: DemoService,
              private modalService: NgbModal) {
    this.handleStatistic();
  }

  open(content) {
    this.modalService.open(content, {size: 'lg', ariaLabelledBy: 'modal-basic-title'}).result.then((result) => {
      this.closeResult = `Closed with: ${result}`;
    }, (reason) => {
      this.closeResult = `Dismissed ${this.getDismissReason(reason)}`;
    });
  }

  private getDismissReason(reason: any): string {
    if (reason === ModalDismissReasons.ESC) {
      return 'by pressing ESC';
    } else if (reason === ModalDismissReasons.BACKDROP_CLICK) {
      return 'by clicking on a backdrop';
    } else {
      return  `with: ${reason}`;
    }
  }

  ngOnInit() {

    let map;

    map = new google.maps.Map(document.getElementById('map'), {
      center: {lat: 41.051268, lng: 37.411134},
      zoom: 15
    });

    var bony = {lat: 48.179519, lng: 16.326289};


    if (this.statisticData.hasOwnProperty('numOfGooglePlaces')) {
      this.numberOfGooglePlaces = this.statisticData["numOfGooglePlaces"];
      this.numberOfOpenPlaces = this.statisticData["numOfOpenstreetMapPlaces"];
    }



    // Create the places service.
    var service = new google.maps.places.PlacesService(map);
    var getNextPage = null;
    var totalGoogle = 0;

    if (getNextPage) getNextPage();

    // Perform a nearby search.
    service.nearbySearch(
      {location: bony, radius: 50},
      function(results, status, pagination) {
        if (status.toString() !== 'OK') return;

        console.info(results);
        totalGoogle += results.length;

        getNextPage = pagination.hasNextPage && function() {
          pagination.nextPage();
        };
      });

    if (this.data !== undefined) {
      if (this.data.body !== undefined) {

        this.data = JSON.parse(this.data.body)

        let keys = Object.keys(this.data);

        let splitCoordinateFocus = keys[0].split(',');

        const FOCUS = {lat: Number(splitCoordinateFocus[0]), lng: Number(splitCoordinateFocus[1])};
        debugger

        map = new google.maps.Map(document.getElementById('map'), {
          center: FOCUS,
          zoom: 15
        });

        var infowindow = new google.maps.InfoWindow();

        var marker;
        var i = 0;

        for (let coordinate of keys) {
          //coordinate = 123.3 80.3
          let value = this.data[coordinate];
          //value = google : name
          // open : name

          let googleName = value["google"];
          let openName = value["openstreet"];
          let foursqName = value["foursquare"];
          let microsoftName = value["microsoft"];


          let splitCoordinate = coordinate.split(',');

          let lat = splitCoordinate[0];
          let lon = splitCoordinate[1];

          let openStreetMapLink = "<a target=\"_blank\" href=\"https://www.openstreetmap.org/#map=18/@LAT/@LON\">" +
            "Open openstreetMap<a/>";
          openStreetMapLink = openStreetMapLink
            .replace("@LAT", lat)
            .replace("@LON", lon);

          console.info(openStreetMapLink);

          marker = new google.maps.Marker({
            position: new google.maps.LatLng(Number(lat), Number(lon)),
            map: map
          });

          marker.setMap(map);

          google.maps.event.addListener(marker, 'click', (function(marker, i) {
            return function() {
              infowindow.setContent('location(' + lat + "," + lon + ") - " + openStreetMapLink + "<br />" + "google:" +
                " " + googleName +
                "<br />" + "open: " +openName + "<br />" + "foursquare: " + foursqName
                + "<br />" + "microsoft: " + microsoftName);

              infowindow.open(map, marker);
            }
          })(marker, i));

          i++;

        }

      }
    }

  }

  handleStatistic() {
    this.demoService.statistic().subscribe(
      // the first argument is a function which runs on success
      data => { console.log('check THIS!!' + data); this.statisticData = data
      },
      // the second argument is a function which runs on error
      err => console.error(err),
      // the third argument is a function which runs on completion
      () => { console.log('done loading data'); this.ngOnInit();}
    );
  }

  handleFileInput(files: FileList) {

    console.info("in post and get");

    this.fileToUpload = files.item(0);
    this.isFileUploaded = true;

    this.demoService.upload(this.fileToUpload).subscribe(
      // the first argument is a function which runs on success
      data => { console.log('check THIS!!' + data); this.data = data
      },
      // the second argument is a function which runs on error
      err => console.error(err),
      // the third argument is a function which runs on completion
      () => { console.log('done loading data'); this.ngOnInit(); }
    );


    console.info("out getdata");


  }
}
