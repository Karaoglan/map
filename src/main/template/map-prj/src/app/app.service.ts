import { Injectable } from '@angular/core';
import {HttpClient, HttpRequest, HttpEvent, HttpResponse, HttpParams} from '@angular/common/http';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/catch';

@Injectable()
export class DemoService {

  private articleUrl = "http://localhost:7090/maps";

  constructor(private http: HttpClient) {
  }

  statistic(rectangle): any {
    console.info(rectangle);

    //let params = new HttpParams().set("rectangle", rectangle);

    let params: URLSearchParams = new URLSearchParams();
    params.set('minLatitude', rectangle.minlat);
    params.set('minLongitude', rectangle.minlon);
    params.set('maxLatitude', rectangle.maxlat);
    params.set('maxLongitude', rectangle.maxlon);

    return this.http.get(this.articleUrl, {search: params})
      .map(this.extractData)
      .catch(this.handleError);
  }

  private extractData(res: HttpResponse<any>) {
    return res;
  }

  private handleError (error: Response | any) {
    console.error(error.message || error);
    return Observable.throw(error.status);
  }

  upload(file: File): Observable<HttpEvent<{}>>{
    let formdata: FormData = new FormData();

    formdata.append('file', file);

    const req = new HttpRequest('POST', this.articleUrl, formdata, {
      reportProgress: true,
      responseType: 'text'
    });

    return this.http.request(req)
      .map(this.extractData)
      .catch(this.handleError)
  }

}
