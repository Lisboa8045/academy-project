import {Component, OnInit} from '@angular/core';
import {createChat} from '@n8n/chat';
import {environment} from '../../enviroments/environments';

@Component({
  selector: 'app-chatbot',
  imports: [],
  templateUrl: './app-chatbot.component.html',
})
export class AppChatbotComponent implements OnInit{

    ngOnInit(): void {
      createChat({
        webhookUrl: environment.chatbotUrl
      });
    }

}
